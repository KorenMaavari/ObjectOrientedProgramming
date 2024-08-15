package OOP.Solution;

import OOP.Provided.OOPExpectedException;

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


    @Override
    public OOPExpectedException expect(Class<? extends Exception> expected) {
        this.expectedException = expected;
        return this;
    }

    @Override
    public OOPExpectedException expectMessage(String msg) {
        this.expectedMessages.add(msg);
        return this;
    }

    @Override
    public boolean assertExpected(Exception e) {
        if (expectedException == null || !expectedException.isInstance(e)) {
            return false;
        }

        String exceptionMessage = e.getMessage();
        if (exceptionMessage == null) {
            exceptionMessage = ""; // Consider an empty string if the message is null
        }

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
