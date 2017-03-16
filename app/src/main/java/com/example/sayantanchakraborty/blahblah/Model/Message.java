package com.example.sayantanchakraborty.blahblah.Model;

import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;

/**
 * Created by sayantanchakraborty on 25/02/17.
 */

public class Message extends Object implements Serializable {
    public String SenderFrom;
    public String SenderTo;
    public String Text;
    public long Timestamp;
    public String ImageUrl;
    public String MessageType;
    public long ImageHeight;
    public long ImageWidth;

    public Message() {
    }

    public Message(String senderFrom, String senderTo, String text, int timestamp, String imageUrl, String messageType, int imageHeight, int imageWidth) {
        SenderFrom = senderFrom;
        SenderTo = senderTo;
        Text = text;
        Timestamp = timestamp;
        ImageUrl = imageUrl;
        MessageType = messageType;
        ImageHeight = imageHeight;
        ImageWidth = imageWidth;
    }

    public String getSenderFrom() {
        return SenderFrom;
    }

    public void setSenderFrom(String senderFrom) {
        this.SenderFrom = senderFrom;
    }

    public String getSenderTo() {
        return SenderTo;
    }

    public void setSenderTo(String senderTo) {
        this.SenderTo = senderTo;
    }

    public String getText() {
        return Text;
    }

    public void setText(String text) {
        this.Text = text;
    }

    public long getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.Timestamp = timestamp;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.ImageUrl = imageUrl;
    }

    public String getMessageType() {
        return MessageType;
    }

    public void setMessageType(String messageType) {
        this.MessageType = messageType;
    }

    public long getImageHeight() {
        return ImageHeight;
    }

    public void setImageHeight(long imageHeight) {
        this.ImageHeight = imageHeight;
    }

    public long getImageWidth() {
        return ImageWidth;
    }

    public void setImageWidth(long imageWidth) {
        this.ImageWidth = imageWidth;
    }

    public String chatPartnerId(){
        if (SenderFrom.contentEquals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
            return SenderTo;
        else
            return SenderFrom;
    }
}
