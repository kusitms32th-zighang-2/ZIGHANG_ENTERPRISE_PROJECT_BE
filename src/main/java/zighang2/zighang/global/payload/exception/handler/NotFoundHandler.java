package zighang2.zighang.global.payload.exception.handler;

import zighang2.zighang.global.payload.code.BaseErrorCode;
import zighang2.zighang.global.payload.exception.GeneralException;

public class NotFoundHandler extends GeneralException {
    public NotFoundHandler(BaseErrorCode errorCode) {super(errorCode);}
}
