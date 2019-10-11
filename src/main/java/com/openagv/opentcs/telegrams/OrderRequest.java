package com.openagv.opentcs.telegrams;

import java.io.Serializable;

public class OrderRequest<T> extends AbsRequest {

    public OrderRequest() {
        super(TelegramType.ORDER);
    }

    public OrderRequest(String telegram) {
        super(TelegramType.ORDER);
        this.originalTelegram = telegram;
    }

    public void setOriginalTelegram(String telegram){
        super.originalTelegram = telegram;
    }

    @Override
    public void setCmdKey(String target) {
        super.target = target;
    }

}
