package OOP.Solution;

import OOP.Provided.OOPExpectedException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;


public class OOPExpectedExceptionImpl implements OOPExpectedException {

    private Class<? extends Exception> expectedException;
    private List<String> expectedMessages;

    public OOPExpectedExceptionImpl() {
        this.expectedException = null;
        this.expectedMessages = new ArrayList<>();
    }

    @Override
    public Class<? extends Exception> getExpectedException() {
        return expectedException;
    }


    public OOPExpectedException expect(Class<? extends Exception> expected) {
        this.expectedException = expected;
        return this;
    }

    public OOPExpectedException expectMessage(String msg) {
        this.expectedMessages.add(msg);
        return this;
    }

    public boolean assertExpected(Exception e) {
        // Check that the exception that was thrown, and passed as parameter, is of a type as expected
        if (expectedException != null && !expectedException.isInstance(e)) {
            return false;
        }

//        // Check expected message are contained in the exception message
//        if (expectedMessages.isEmpty()) {
//            return true;
//        }

        String exceptionMessage = e.getMessage();
        for (String expectedMessage : expectedMessages) {
            if (expectedMessage == null || !exceptionMessage.contains(expectedMessage)) {
                return false;
            }
        }

        return true;
    }

    public static OOPExpectedException none() {
        return new OOPExpectedExceptionImpl();
    }
}
