package OOP.Solution;

import OOP.Provided.OOPResult;
import java.util.Map;

public class OOPTestSummary {
    private Map<String, OOPResult> testMap;

    public OOPTestSummary(Map<String, OOPResult> testMap) {
        this.testMap = testMap;
    }

    int getNumOOPTestResult (OOPResult.OOPTestResult OOPTestResult) {
        return (int) testMap.values().stream()
                .filter(result -> result.getResultType() == OOPTestResult)
                .count();
    }

    public int getNumSuccesses() {
        return getNumOOPTestResult(OOPResult.OOPTestResult.SUCCESS);
    }

    public int getNumFailures() {
        return getNumOOPTestResult(OOPResult.OOPTestResult.FAILURE);
    }

    public int getNumExceptionMismatches() {
        return getNumOOPTestResult(OOPResult.OOPTestResult.EXPECTED_EXCEPTION_MISMATCH);
    }

    public int getNumErrors() {
        return getNumOOPTestResult(OOPResult.OOPTestResult.ERROR);
    }
}
