package OOP.Solution;

import OOP.Provided.OOPAssertionFailure;
import OOP.Provided.OOPExceptionMismatchError;
import OOP.Provided.OOPExpectedException;
import OOP.Provided.OOPResult;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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

    /**
     * Runs test methods in the specified class that match the provided tag.
     *
     * @param testClass The class containing test methods.
     * @param tag The tag to filter test methods.
     * @return A summary of the test results.
     */
    public static OOPTestSummary runClass(Class<?> testClass, String tag) {
        // Validate that the class is a test class by checking the OOPTestClass annotation
        if (testClass == null || !testClass.isAnnotationPresent(OOPTestClass.class)) {
            throw new IllegalArgumentException("Provided class is not a test class");
        }

        // Get the type of test class (ORDERED or UNORDERED)
        OOPTestClass.OOPTestClassType classType = testClass.getAnnotation(OOPTestClass.class).value();

        // Attempt to create a new instance of the test class
        Object testInstance;
        try {
            testInstance = testClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            // Return null if the instance cannot be created
            return null;
        }

        // Map to store the results of each test method
        Map<String, OOPResult> testResults = new LinkedHashMap<>();

        // Step 1: Run @OOPSetup methods
        runAnnotatedMethods(testInstance, OOPSetup.class);

        // Step 2: Collect and run test methods based on tag and order
        List<Method> testMethods = new ArrayList<>();
        for (Method method : testClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(OOPTest.class)) {
                OOPTest testAnnotation = method.getAnnotation(OOPTest.class);
                // Add method to list if tag matches or is empty
                if (tag.isEmpty() || testAnnotation.tag().equals(tag)) {
                    testMethods.add(method);
                }
            }
        }

        // Order test methods if the class is ORDERED
        if (classType == OOPTestClass.OOPTestClassType.ORDERED) {
            testMethods.sort(Comparator.comparingInt(m -> m.getAnnotation(OOPTest.class).order()));
        }

        // Step 3: Execute test methods
        for (Method testMethod : testMethods) {
            String methodName = testMethod.getName();
            try {
                // Backup state before running before methods
                Object backupInstance = backupState(testInstance);

                try {
                    // Run @OOPBefore methods
                    runAnnotatedMethodsForTest(testInstance, OOPBefore.class, methodName);
                } catch (Exception e) {
                    // Restore state if before methods fail
                    restoreState(testInstance, backupInstance);
                    throw e;
                }

                // Execute the test method
                testMethod.setAccessible(true);
                testMethod.invoke(testInstance);
                // Mark the test as successful if no exception is thrown
                testResults.put(methodName, new OOPResultImpl(OOPResult.OOPTestResult.SUCCESS, null));
            } catch (Exception e) {
                // Handle exceptions and classify results
                Throwable cause = e.getCause();
                OOPExpectedException expectedException = getExpectedExceptionField(testInstance);
                if (expectedException != null) {
                    if (cause instanceof Exception) {
                        Exception exception = (Exception) cause;
                        if (expectedException.assertExpected(exception)) {
                            testResults.put(methodName, new OOPResultImpl(OOPResult.OOPTestResult.SUCCESS, null));
                        } else {
                            testResults.put(methodName, new OOPResultImpl(OOPResult.OOPTestResult.EXPECTED_EXCEPTION_MISMATCH,
                                    new OOPExceptionMismatchError(expectedException.getExpectedException(), exception.getClass()).getMessage()));
                        }
                    } else {
                        // Handle cases where the cause is not an Exception
                        testResults.put(methodName, new OOPResultImpl(OOPResult.OOPTestResult.ERROR, cause.getClass().getName()));
                    }
                } else if (cause instanceof OOPAssertionFailure) {
                    // Mark as failure if an assertion failure occurs
                    testResults.put(methodName, new OOPResultImpl(OOPResult.OOPTestResult.FAILURE, cause.getMessage()));
                } else {
                    // Mark as error for any other exception
                    testResults.put(methodName, new OOPResultImpl(OOPResult.OOPTestResult.ERROR, cause.getClass().getName()));
                }
            }

            try {
                // Ensure @OOPAfter methods are always run
                runAnnotatedMethodsForTest(testInstance, OOPAfter.class, methodName);
            } catch (Exception e) {
                // If after methods fail, the result should be an error
                testResults.put(methodName, new OOPResultImpl(OOPResult.OOPTestResult.ERROR, e.getClass().getName()));
            }
        }

        // Return the summary of test results
        return new OOPTestSummary(testResults);
    }

    /**
     * Runs methods annotated with the specified annotation.
     *
     * @param instance The instance of the test class.
     * @param annotation The annotation to look for.
     */
    private static void runAnnotatedMethods(Object instance, Class<? extends Annotation> annotation) {
        // Traverse the class hierarchy to find and run annotated methods
        Class<?> clazz = instance.getClass();
        while (clazz != null) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(annotation)) {
                    try {
                        method.setAccessible(true);
                        method.invoke(instance);
                    } catch (Exception ignored) {
                        // Koren: what should we do in this case?
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    /**
     * Runs methods annotated with the specified annotation for a specific test method.
     *
     * @param instance The instance of the test class.
     * @param annotation The annotation to look for.
     * @param testName The name of the test method.
     */
    private static void runAnnotatedMethodsForTest(Object instance, Class<? extends Annotation> annotation, String testName) {
        // Traverse the class hierarchy to find and run annotated methods for the specific test
        Class<?> clazz = instance.getClass();
        while (clazz != null) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(annotation)) {
                    try {
                        // Get the value of the annotation which contains the test method names
                        String[] relatedTests = (String[]) method.getAnnotation(annotation).annotationType().getMethod("value").invoke(method.getAnnotation(annotation));
                        if (Arrays.asList(relatedTests).contains(testName)) {
                            method.setAccessible(true);
                            method.invoke(instance);
                        }
                    } catch (Exception ignored) {
                        // Koren: what should we do in this case?
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    /**
     * Retrieves the OOPExpectedException field from the test instance.
     *
     * This method traverses the class hierarchy to find a field annotated with
     * @OOPExceptionRule, which indicates that the field is an instance of OOPExpectedException.
     * It returns the value of this field if found, or null otherwise.
     *
     * @param instance The instance of the test class.
     * @return The OOPExpectedException field, or null if not found.
     */
    private static OOPExpectedException getExpectedExceptionField(Object instance) {
        // Start with the class of the given instance
        Class<?> clazz = instance.getClass();

        // Traverse the class hierarchy
        while (clazz != null) {
            // Iterate over all declared fields of the current class
            for (Field field : clazz.getDeclaredFields()) {
                // Check if the field is annotated with @OOPExceptionRule
                if (field.isAnnotationPresent(OOPExceptionRule.class)) {
                    try {
                        // Make the field accessible if it is private
                        field.setAccessible(true);
                        // Return the value of the field if it is an OOPExpectedException
                        return (OOPExpectedException) field.get(instance);
                    } catch (IllegalAccessException ignored) {
                        // If we cannot access the field, ignore the exception and continue
                    }
                }
            }
            // Move to the superclass to continue searching
            clazz = clazz.getSuperclass();
        }
        // Return null if no field annotated with @OOPExceptionRule is found
        return null;
    }

    /**
     * Backs up the state of the instance.
     *
     * @param instance The instance of the test class.
     * @return A backup of the instance state.
     * @throws Exception If an error occurs during backup.
     */
    private static Object backupState(Object instance) throws Exception {
        // Create a new instance to hold the backup
        Class<?> clazz = instance.getClass();
        Object backupInstance = clazz.getDeclaredConstructor().newInstance();

        // Traverse the class hierarchy to back up fields
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(instance);
                if (value != null) {
                    // Try to clone the field value if it implements Cloneable
                    if (value instanceof Cloneable) {
                        Method cloneMethod = value.getClass().getMethod("clone");
                        field.set(backupInstance, cloneMethod.invoke(value));
                    } else {
                        // Try to use a copy constructor if available
                        try {
                            Constructor<?> copyConstructor = value.getClass().getConstructor(value.getClass());
                            field.set(backupInstance, copyConstructor.newInstance(value));
                        } catch (NoSuchMethodException e) {
                            // If neither clone nor copy constructor are available, use the original value
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

    /**
     * Restores the state of the instance from the backup.
     *
     * @param instance The instance of the test class.
     * @param backupInstance The backup instance to restore from.
     * @throws Exception If an error occurs during restoration.
     */
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
}
