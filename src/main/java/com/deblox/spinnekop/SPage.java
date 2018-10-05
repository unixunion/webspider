package com.deblox.spinnekop;

import io.vertx.core.json.JsonObject;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;

public class SPage {

    public enum State {
        NEW, PROCESSED, PROCESSING_CHILDREN, COMPLETE
    }

    private String url;
    private List<String> sublinks;
    private String body;
    private State state;
    private long lastCheckTime;
    private int rank;

    public SPage(String url) {
        this.url = url;
        body = "";
        state = State.NEW;
        sublinks = new ArrayList<>();
        rank = 0;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
        lastCheckTime = Calendar.getInstance().getTimeInMillis();
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getLinkHash() {
        try {
            return StringHasher.sha1(url);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addSublink(String link) {
        sublinks.add(link);
    }

    public void setSublinks(List<String> sublinks1) {
        sublinks = sublinks1;
    }

    public List<String> getSublinks() {
        return sublinks;
    }

    public long getLastCheckTime() {
        return lastCheckTime;
    }

    public void setLastCheckTime(long t) {
        lastCheckTime = t;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public JsonObject serialize() {
        JsonObject j = new JsonObject().put("url", url)
                .put("sublinks", sublinks)
                .put("body", Base64.getEncoder().encode(body.getBytes()))
                .put("state", state)
                .put("lastCheckTime", lastCheckTime)
                .put("rank", rank);
        return j;
    }

    public static SPage fromJson(JsonObject json) {
        SPage spage = new SPage(json.getString("url"));
        json.getJsonArray("sublinks").forEach(link -> {
            spage.addSublink((String)link);
        });
        String body = new String(Base64.getDecoder().decode(json.getBinary("body")));
        spage.setBody(body);
        spage.setState(State.valueOf(json.getString("state")));
        spage.setLastCheckTime(json.getLong("lastCheckTime"));
        spage.setRank(json.getInteger("rank"));
        return spage;
    }

}
