package edu.cmu.linquanc.exception;

/**
 * Created by chenlinquan on 11/16/15.
 */
public class MyExcaption extends Exception{
    /** Error Number*/
    private  int errorNumber;
    /** Error Message*/
    private String errorMessage;

    public MyExcaption(int errorNumber) {
        this.errorNumber = errorNumber;
        this.errorMessage = "Can not get the location, Please check your device!";
    }

    @Override
    public String toString() {
        return "MyException: errorNumber = " + errorNumber
                + ", errorMessgae = " + errorMessage;

    }

}
