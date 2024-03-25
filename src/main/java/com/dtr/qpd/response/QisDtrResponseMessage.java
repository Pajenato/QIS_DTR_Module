package com.dtr.qpd.response;

public class QisDtrResponseMessage {
    private String message;

    public QisDtrResponseMessage(){

    }

    public QisDtrResponseMessage(String message){
        super();
        this.setMessage(message);
    }

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
    
}
