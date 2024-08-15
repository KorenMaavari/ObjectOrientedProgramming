package OOP.Solution;

import OOP.Provided.OOPAssertionFailure;
import OOP.Provided.OOPExceptionMismatchError;
import OOP.Provided.OOPExpectedException;
import OOP.Provided.OOPResult;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Main class for the custom unit testing framework.
 * Handles the execution and reporting of test methods in test classes.
 */
public class OOPUnitCore {

    /**
     * Asserts that the expected and actual objects are equal.
     * Throws OOPAssertionFailure if they are not.
     *
     * @param expected The expected object.
     * @param actual The actual object.
     * @throws OOPAssertionFailure If the objects are not equal.
     */
    public static void assertEquals(Object expected, Object actual) throws OOPAssertionFailure {
        // Use Objects.equals to handle null cases and deep equality checks
        if (!Objects.equals(expected, actual)) {
            throw new OOPAssertionFailure(expected, actual);
        }
    }

    /**
     * Marks a test as failed by throwing an OOPAssertionFailure.
     *
     * @throws OOPAssertionFailure Always thrown to indicate failure.
     */
    public static void fail() throws OOPAssertionFailure {
        throw new OOPAssertionFailure();
    }

    /**
     * Runs all test methods in the specified class.
     *
     * @param testClass The class containing test methods.
     * @return A summary of the test results.
     */
    public static OOPTestSummary runClass(Class<?> testClass) {
        // Call the overloaded method with an empty tag to run all test methods
        return runClass(testClass, "");
    }

    public static OOPTestSummary runClass(Class<?> testClass, String tag) {
        if (testClass == null || !testClass.isAnnotationPresent(OOPTestClass.class)) {
            throw new IllegalArgumentException("Provided class is not a test class");
        }

        OOPTestClass.OOPTestClassType classType = testClass.getAnnotation(OOPTestClass.class).value();
        Object testInstance;
        try {
            testInstance = testClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace(); // Added logging
            return null;
        }

        Map<String, OOPResult> testResults = new LinkedHashMap<>();
        System.out.println("Running @OOPSetup methods");
        runAnnotatedMethods(testInstance, OOPSetup.class);

        List<Method> testMethods = new ArrayList<>();
        collectTestMethods(testMethods, testClass, tag);

        if (classType == OOPTestClass.OOPTestClassType.ORDERED) {
            testMethods.sort(Comparator.comparingInt(m -> m.getAnnotation(OOPTest.class).order()));
        }

        System.out.println("Collected test methods: " + testMethods);

        for (Method testMethod : testMethods) {
            String methodName = testMethod.getName();
            System.out.println("Executing test method: " + methodName);
            OOPResultImpl result;

            try {
                Object backupInstance = backupState(testInstance);

                try {
                    System.out.println("Running @OOPBefore methods for: " + methodName);
                    runAnnotatedMethodsForTest(testInstance, OOPBefore.class, methodName);
                } catch (Exception e) {
                    System.out.println("Exception in @OOPBefore for: " + methodName);
                    e.printStackTrace();
                    restoreState(testInstance, backupInstance);
                    result = new OOPResultImpl(OOPResult.OOPTestResult.ERROR, e.getClass().getName());
                    System.out.println("ERROR#1\n");
                    testResults.put(methodName, result);
                    continue;
                }

                try {
                    testMethod.setAccessible(true);
                    testMethod.invoke(testInstance);

                    // Check if an exception was expected but not thrown
                    OOPExpectedException expectedException = getExpectedExceptionField(testInstance);
                    if (expectedException != null && expectedException.getExpectedException() != null) {
                        System.out.println("Expected exception was not thrown for: " + methodName);
                        result = new OOPResultImpl(OOPResult.OOPTestResult.ERROR, expectedException.getExpectedException().getName());
                        System.out.println("ERROR#2\n");
                    } else {
                        System.out.println("Test method succeeded: " + methodName);
                        result = new OOPResultImpl(OOPResult.OOPTestResult.SUCCESS, null);
                        System.out.println("SUCCESS#1\n");
                    }
                } catch (Exception e) {
                    Throwable cause = e.getCause();
                    OOPExpectedException expectedException = getExpectedExceptionField(testInstance);

                    if (expectedException != null && cause instanceof Exception) {
                        Exception exception = (Exception) cause;
                        if (expectedException.assertExpected(exception)) {
                            System.out.println("Expected exception thrown for: " + methodName);
                            result = new OOPResultImpl(OOPResult.OOPTestResult.SUCCESS, null);
                            System.out.println("SUCCESS#2\n");
                        } else {
                            System.out.println("Expected exception mismatch for: " + methodName);
                            result = new OOPResultImpl(OOPResult.OOPTestResult.EXPECTED_EXCEPTION_MISMATCH,
                                    new OOPExceptionMismatchError(expectedException.getExpectedException(), exception.getClass()).getMessage());
                            System.out.println("MISMATCH#1\n");
                        }
                    } else if (cause instanceof OOPAssertionFailure) {
                        System.out.println("Assertion failure in test: " + methodName);
                        result = new OOPResultImpl(OOPResult.OOPTestResult.FAILURE, cause.getMessage());
                        System.out.println("FAILURE#1\n");
                    } else {
                        System.out.println("Unexpected error in test: " + methodName);
                        result = new OOPResultImpl(OOPResult.OOPTestResult.ERROR, cause.getClass().getName());
                        System.out.println("ERROR#3\n");
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                result = new OOPResultImpl(OOPResult.OOPTestResult.ERROR, e.getClass().getName());
                System.out.println("ERROR#4\n");
            }

            try {
                System.out.println("Running @OOPAfter methods for: " + methodName);
                runAnnotatedMethodsForTest(testInstance, OOPAfter.class, methodName);
            } catch (Exception e) {
                System.out.println("Exception in @OOPAfter for: " + methodName);
                e.printStackTrace();
                result = new OOPResultImpl(OOPResult.OOPTestResult.ERROR, e.getClass().getName());
                System.out.println("ERROR#5\n");
            }

            // Ensure the result is stored in the map with a unique reference
            testResults.put(methodName, result);
        }

        System.out.println("Test results: " + testResults);
        return new OOPTestSummary(testResults);
    }



    /**
     * Collects test methods based on the provided tag.
     *
     * @param testMethods The list to store collected test methods.
     * @param testClass The class containing test methods.
     * @param tag The tag to filter test methods.
     */
    private static void collectTestMethods(List<Method> testMethods, Class<?> testClass, String tag) {
        // Traverse the class hierarchy to collect test methods
        Class<?> clazz = testClass;
        while (clazz != null) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(OOPTest.class)) {
                    OOPTest testAnnotation = method.getAnnotation(OOPTest.class);
                    // Add method to list if tag matches or is empty
                    if (tag.isEmpty() || testAnnotation.tag().equals(tag)) {
                        testMethods.add(method);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    private static void runAnnotatedMethodsForTest(Object instance, Class<? extends Annotation> annotation, String testName) {
        Class<?> clazz = instance.getClass();
        while (clazz != null) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(annotation)) {
                    try {
                        String[] relatedTests = (String[]) method.getAnnotation(annotation).annotationType().getMethod("value").invoke(method.getAnnotation(annotation));
                        if (Arrays.asList(relatedTests).contains(testName)) {
                            System.out.println("Running " + annotation.getSimpleName() + " method: " + method.getName() + " for test: " + testName);
                            method.setAccessible(true);
                            method.invoke(instance);
                        }
                    } catch (Exception e) {
                        e.printStackTrace(); // Added logging
                        if (annotation == OOPBefore.class) {
                            try {
                                throw e;
                            } catch (IllegalAccessException ex) {
                                throw new RuntimeException(ex);
                            } catch (InvocationTargetException ex) {
                                throw new RuntimeException(ex);
                            } catch (NoSuchMethodException ex) {
                                throw new RuntimeException(ex);
                            }
                        } else {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    private static void runAnnotatedMethods(Object instance, Class<? extends Annotation> annotation) {
        Class<?> clazz = instance.getClass();
        while (clazz != null) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(annotation)) {
                    try {
                        System.out.println("Running " + annotation.getSimpleName() + " method: " + method.getName());
                        method.setAccessible(true);
                        method.invoke(instance);
                    } catch (Exception e) {
                        e.printStackTrace(); // Added logging
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    private static Object backupState(Object instance) throws Exception {
        Class<?> clazz = instance.getClass();
        Object backupInstance = clazz.getDeclaredConstructor().newInstance();

        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(instance);
                if (value != null) {
                    if (value instanceof Cloneable) {
                        Method cloneMethod = value.getClass().getMethod("clone");
                        field.set(backupInstance, cloneMethod.invoke(value));
                    } else {
                        try {
                            Constructor<?> copyConstructor = value.getClass().getConstructor(value.getClass());
                            field.set(backupInstance, copyConstructor.newInstance(value));
                        } catch (NoSuchMethodException e) {
                            field.set(backupInstance, value);
                        }
                    }
                } else {
                    field.set(backupInstance, null);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return backupInstance;
    }

    private static void restoreState(Object instance, Object backupInstance) throws Exception {
        Class<?> clazz = instance.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                field.set(instance, field.get(backupInstance));
            }
            clazz = clazz.getSuperclass();
        }
    }

    private static OOPExpectedException getExpectedExceptionField(Object instance) {
        Class<?> clazz = instance.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(OOPExceptionRule.class)) {
                    try {
                        field.setAccessible(true);
                        return (OOPExpectedException) field.get(instance);
                    } catch (IllegalAccessException ignored) {
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }
}
