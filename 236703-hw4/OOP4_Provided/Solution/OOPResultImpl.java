package OOP.Solution;

import OOP.Provided.OOPResult;

import java.util.Objects;


public class OOPResultImpl implements OOPResult {
    private OOPTestResult resultType;
    private String message;


    public OOPResultImpl(OOPTestResult resultType, String message) {
        this.resultType = resultType;
        this.message = message;
    }

    @Override
    public OOPTestResult getResultType() {
        return resultType;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OOPResultImpl that = (OOPResultImpl) obj;
        // Handle the cases when the objects are nulls
        if (resultType != that.resultType) return false;
        return message != null ? message.equals(that.message) : that.message == null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(resultType, message);
    }

}
