package solution;

import provided.StoryTestException;

public class StoryTestExceptionImpl extends StoryTestException {
    private int numFails;
    private String firstFailedSentence;
    private String expected;
    private String result;

    public StoryTestExceptionImpl(int numFails, String firstFailedSentence, String expected, String result) {
        this.numFails = numFails;
        this.firstFailedSentence = firstFailedSentence;
        this.expected = expected;
        this.result = result;
    }

    @Override
    public String getSentance() {
        return firstFailedSentence;
    }

    @Override
    public String getStoryExpected() {
        return expected;
    }

    @Override
    public String getTestResult() {
        return result;
    }

    @Override
    public int getNumFail() {
        return numFails;
    }
}
