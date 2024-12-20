package com.example.lab6.domain;

import java.time.LocalDateTime;
import java.util.List;

public class PhotoMessage extends AbstractMessage{
    private byte[] photo;

    public PhotoMessage(Utilizator sender, List<Utilizator> receivers, byte[] photo, LocalDateTime date) {
        super(sender, receivers, date);
        this.photo = photo;
    }

    public byte[] getPhoto(){
        return photo;
    }

    public void setPhoto(byte[] photo){
        this.photo = photo;
    }
}
