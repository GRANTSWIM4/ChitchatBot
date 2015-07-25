package com.demigodsrpg.chitchatbot.ai;

import java.io.Serializable;

public class Quad<T extends Serializable> implements Serializable {
    String id;
    T t1, t2, t3, t4;
    boolean canStart = false;
    boolean canEnd = false;

    public Quad() {
    }

    public Quad(T t1, T t2, T t3, T t4) {
        this.id = t1.toString() + t2.toString() + t3.toString() + t4.toString();
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = t3;
        this.t4 = t4;
    }

    public T getToken(int index) {
        switch (index) {
            case 0:
                return t1;
            case 1:
                return t2;
            case 2:
                return t3;
            case 3:
                return t4;
        }
        return null;
    }
    
    public void setCanStart(boolean flag) {
        canStart = flag;
    }
    
    public void setCanEnd(boolean flag) {
        canEnd = flag;
    }    
    
    public boolean canStart() {
        return canStart;
    }
    
    public boolean canEnd() {
        return canEnd;
    }

    public boolean isValid() {
        return t1 != null && t2 != null && t3 != null && t4 != null;
    }

    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return t1.hashCode() +
                t2.hashCode() +
                t3.hashCode() +
                t4.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if(o instanceof Quad) {
            Quad other = (Quad) o;
            return other.t1.equals(t1) &&
                    other.t2.equals(t2) &&
                    other.t3.equals(t3) &&
                    other.t4.equals(t4);
        }
        return false;
    }
}