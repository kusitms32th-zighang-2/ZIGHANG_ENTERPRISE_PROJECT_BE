
package zighang2.zighang.global.payload.exception.handler;


import zighang2.zighang.global.payload.code.BaseErrorCode;
import zighang2.zighang.global.payload.exception.GeneralException;

public class TempHandler extends GeneralException {

    public TempHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
