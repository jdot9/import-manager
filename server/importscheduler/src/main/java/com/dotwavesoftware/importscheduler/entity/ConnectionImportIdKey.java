package com.dotwavesoftware.importscheduler.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionImportIdKey implements Serializable {
    
    private int sendingConnectionId;
    private int receivingConnectionId;
    private int importId;
    private String sendingConnectionFieldName;
    private String receivingConnectionFieldName;
        
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionImportIdKey that = (ConnectionImportIdKey) o;
        return sendingConnectionId == that.sendingConnectionId 
            && receivingConnectionId == that.receivingConnectionId 
            && importId == that.importId
            && Objects.equals(sendingConnectionFieldName, that.sendingConnectionFieldName)
            && Objects.equals(receivingConnectionFieldName, that.receivingConnectionFieldName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(sendingConnectionId, receivingConnectionId, importId, 
            sendingConnectionFieldName, receivingConnectionFieldName);
    }
}
