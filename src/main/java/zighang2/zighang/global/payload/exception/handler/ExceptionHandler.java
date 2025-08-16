package zighang2.zighang.global.payload.exception.handler;


import zighang2.zighang.global.payload.code.BaseErrorCode;
import zighang2.zighang.global.payload.exception.GeneralException;

public class ExceptionHandler extends GeneralException {
    public ExceptionHandler(BaseErrorCode code) {
        super(code);
    }
}