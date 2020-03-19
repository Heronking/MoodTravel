package com.wangliu.moodtravel.widget;

import com.amap.api.services.route.BusStep;

public class BusPlan extends BusStep {
    private boolean isWalk;
    private boolean isBus;
    private boolean isRailway;
    private boolean isTaxi;
    private boolean isStart;
    private boolean isEnd;

    public boolean isWalk() {
        return isWalk;
    }

    public void setWalk(boolean walk) {
        isWalk = walk;
    }

    public boolean isBus() {
        return isBus;
    }

    public void setBus(boolean bus) {
        isBus = bus;
    }

    public boolean isRailway() {
        return isRailway;
    }

    public void setRailway(boolean railway) {
        isRailway = railway;
    }

    public boolean isTaxi() {
        return isTaxi;
    }

    public void setTaxi(boolean taxi) {
        isTaxi = taxi;
    }

    public boolean isStart() {
        return isStart;
    }

    public void setStart(boolean start) {
        isStart = start;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }

    public BusPlan(BusStep step) {
        if (step != null) {
            this.setBusLines(step.getBusLines());
            this.setWalk(step.getWalk());
            this.setTaxi(step.getTaxi());
            this.setRailway(step.getRailway());
        }
    }

}
