package solution;

import org.junit.ComparisonFailure;
import provided.*;

import java.lang.annotation.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Implementation of the StoryTester interface for testing stories based on given, when, and then annotations.
 * Provides methods to test scenarios described in natural language against Java classes.
 */
public class StoryTesterImpl implements StoryTester {

    private Object objectBackup; // Backup of the current object state for restoration during tests.
    String firstFailedSentence; // The first sentence in the story that failed the test.
    String expected; // Expected outcome for the failing test.
    String result; // Actual outcome for the failing test.
    int numFails; // Number of failed test sentences.

    /**
     * Creates and returns a new instance of the specified test class.
     *
     * @param testClass The class to instantiate.
     * @return A new instance of the test class.
     * @throws Exception If the class cannot be instantiated.
     */
    private static Object createTestInstance(Class<?> testClass) throws Exception {
        Constructor<?> ctor;
        try {
            // Attempt to retrieve and invoke a parameterless constructor.
            ctor = testClass.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (Exception e) {
            // Handle cases for inner classes requiring an instance of the enclosing class.
            Class<?> enclosingClass = testClass.getEnclosingClass();
            Object enclosingInstance = createTestInstance(enclosingClass);
            ctor = testClass.getDeclaredConstructor(enclosingClass);
            ctor.setAccessible(true);
            return ctor.newInstance(enclosingInstance);
        }
    }

    /**
     * Checks if a given class has a copy constructor.
     *
     * @param c The class to check.
     * @return True if the class has a copy constructor, false otherwise.
     */
    private boolean copyConstructorExists(Class<?> c) {
        try {
            c.getDeclaredConstructor(c); // Try finding a copy constructor.
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Creates a backup of the given object's state and stores it in objectBackup.
     *
     * @param obj The object to back up.
     * @throws Exception If backup fails.
     */
    private void backUpInstance(Object obj) throws Exception {
        Object backup = createTestInstance(obj.getClass());
        Field[] fieldsArr = obj.getClass().getDeclaredFields();

        for (Field field : fieldsArr) {
            field.setAccessible(true); // Make private fields accessible.
            Object fieldValue = field.get(obj);

            if (fieldValue == null) {
                field.set(backup, null);
                continue;
            }

            Class<?> fieldClass = fieldValue.getClass();

            if (fieldValue instanceof Cloneable) {
                // Handle cloneable fields.
                Method cloner = fieldClass.getDeclaredMethod("clone");
                cloner.setAccessible(true);
                field.set(backup, cloner.invoke(fieldValue));
            } else if (copyConstructorExists(fieldClass)) {
                // Handle fields with copy constructors.
                Constructor<?> cpyConstructor = fieldClass.getDeclaredConstructor(fieldClass);
                cpyConstructor.setAccessible(true);
                field.set(backup, cpyConstructor.newInstance(fieldValue));
            } else {
                // Handle other types by direct assignment.
                field.set(backup, fieldValue);
            }
        }

        this.objectBackup = backup;
    }

    /**
     * Searches for a method in the inheritance tree with the specified annotation and value.
     *
     * @param testClass  The class to search.
     * @param annotation The annotation name ("Given", "When", "Then").
     * @param value      The value of the annotation to match.
     * @return The matching method, or null if not found.
     */
    public static Method searchInheritance(Class<?> testClass, String annotation, String value) {
        for (Method method : testClass.getDeclaredMethods()) {
            if (hasAnnotation(method, annotation, value)) return method;
        }

        if (testClass != Object.class) {
            return searchInheritance(testClass.getSuperclass(), annotation, value);
        }
        return null;
    }

    /**
     * Restores the state of an object from objectBackup.
     *
     * @param obj The object to restore.
     * @throws Exception If restoration fails.
     */
    private void restoreInstance(Object obj) throws Exception {
        Field[] fieldsArr = obj.getClass().getDeclaredFields();

        for (Field field : fieldsArr) {
            field.setAccessible(true); // Make private fields accessible.
            field.set(obj, field.get(objectBackup));
        }
    }

    /**
     * Retrieves the annotation class corresponding to the given annotation name.
     *
     * @param annotationName The name of the annotation ("Given", "When", "Then").
     * @return The corresponding annotation class.
     */
    private static Class<? extends Annotation> GetAnnotationClass(String annotationName) {
        return switch (annotationName) {
            case "Given" -> Given.class;
            case "When" -> When.class;
            default -> Then.class;
        };
    }

    /**
     * Tests a story against the inheritance tree of the given class.
     *
     * @param story     The story to test.
     * @param testClass The class to test against.
     * @throws Exception If an error occurs during testing.
     */
    @Override
    public void testOnInheritanceTree(String story, Class<?> testClass) throws Exception {
        if ((story == null) || testClass == null) throw new IllegalArgumentException();

        int whenCount = 0;
        this.numFails = 0;
        Object testInstance = createTestInstance(testClass);

        for (String sentence : story.split("\n")) {
            String[] words = sentence.split(" ", 2);

            String annotationName = words[0];
            String sentenceSub = words[1].substring(0, words[1].lastIndexOf(' '));
            String parameter = sentence.substring(sentence.lastIndexOf(' ') + 1);

            Method method = searchInheritance(testClass, annotationName, sentenceSub);
            if (method == null) {
                throw switch (annotationName) {
                    case "Given" -> new GivenNotFoundException();
                    case "When" -> new WhenNotFoundException();
                    default -> new ThenNotFoundException();
                };
            }

            try {
                if (annotationName.equals("When")) {
                    if (whenCount == 0) backUpInstance(testInstance);
                    whenCount++;
                } else whenCount = 0;

                method.setAccessible(true);

                if (method.getParameterTypes()[0] == Integer.class || method.getParameterTypes()[0] == int.class) {
                    method.invoke(testInstance, Integer.parseInt(parameter));
                } else {
                    method.invoke(testInstance, parameter);
                }
            } catch (InvocationTargetException e) {
                if (!(e.getTargetException() instanceof ComparisonFailure)) throw e;
                if (this.numFails == 0) {
                    this.result = ((ComparisonFailure) e.getTargetException()).getActual();
                    this.expected = ((ComparisonFailure) e.getTargetException()).getExpected();
                    this.firstFailedSentence = sentence;
                }
                this.numFails++;
                if (annotationName.equals("Then")) restoreInstance(testInstance);
            }
        }

        if (this.numFails > 0) {
            throw new StoryTestExceptionImpl(numFails, firstFailedSentence, expected, result);
        }
    }

    /**
     * Tests a story against nested classes in the given class.
     *
     * @param story     The story to test.
     * @param testClass The class containing nested classes to test.
     * @throws Exception If an error occurs during testing.
     */
    @Override
    public void testOnNestedClasses(String story, Class<?> testClass) throws Exception {
        if (testClass == null || story == null) {
            throw new IllegalArgumentException();
        }

        String[] givenAndRest = story.split("\n", 2);
        String aGivenSentence = givenAndRest[0].substring(givenAndRest[0].indexOf(' ') + 1, givenAndRest[0].lastIndexOf(' '));

        Class<?> declaresGiven = searchNestedGiven(aGivenSentence, testClass);
        if (declaresGiven == null) throw new GivenNotFoundException();

        testOnInheritanceTree(story, declaresGiven);
    }

    /**
     * Checks if a method has a specified annotation with a given value.
     *
     * @param method     The method to check.
     * @param annotation The annotation name ("Given", "When", "Then").
     * @param value      The value of the annotation to match.
     * @return True if the method has the annotation with the given value, false otherwise.
     */
    public static boolean hasAnnotation(Method method, String annotation, String value) {
        Class<? extends Annotation> cls = GetAnnotationClass(annotation);
        if (method.getAnnotation(cls) == null) return false;

        String annotationValue = switch (annotation) {
            case "Given" -> ((Given) method.getAnnotation(cls)).value();
            case "When" -> ((When) method.getAnnotation(cls)).value();
            case "Then" -> ((Then) method.getAnnotation(cls)).value();
            default -> null;
        };

        return annotationValue != null && annotationValue.substring(0, annotationValue.lastIndexOf(" ")).equals(value);
    }

    /**
     * Searches for a "Given" method in nested classes.
     *
     * @param value     The "Given" sentence to search for.
     * @param testClass The class containing nested classes.
     * @return The class declaring the "Given" method, or null if not found.
     */
    public static Class<?> searchNestedGiven(String value, Class<?> testClass) {
        if (searchInheritance(testClass, "Given", value) != null) return testClass;

        for (Class<?> nestedClass : testClass.getDeclaredClasses()) {
            if (nestedClass.isInterface()) continue;
            if (searchInheritance(nestedClass, "Given", value) != null) return nestedClass;

            Class<?> result = searchNestedGiven(value, nestedClass);
            if (result != null) return result;
        }
        return null;
    }
}
