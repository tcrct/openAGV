package com.openagv.opentcs.telegrams;

public class OrderRequest extends AbsRequest {

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
    public void setTarget(String target) {
        super.target = target;
    }
}
