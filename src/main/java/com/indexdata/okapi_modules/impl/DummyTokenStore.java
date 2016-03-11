package com.indexdata.okapi_modules;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class DummyTokenStore implements TokenStore {
  private Map<String, List<String>> data;

  public DummyTokenStore() {
    data = new HashMap<String, List<String>>();
  }

  public List<String> getTokenList(String store) {
    if(!data.containsKey(store)) {
      return null;
    }
    return data.get(store);
  }
  
  public boolean hasStore(String store) {
    return data.containsKey(store);
  }

  public boolean hasToken(String token, String store) {
    if(!data.containsKey(store)) {
      return false;
    }
    return data.get(store).contains(token);
  }

  public void addStore(String store) {
    if(data.containsKey(store)) {
        return;
    }
    ArrayList list = new ArrayList();
    data.put(store, list);
  }

  public void addToken(String token, String store) {
    if(data.containsKey(store)) {
      if(!data.get(store).contains(token)) {
        data.get(store).add(token);
      }
    }
  }

  public void deleteToken(String token, String store) {
    if(data.containsKey(store)) {
      if(data.get(store).contains(token)) {
        data.get(store).remove(token);
      }
    }
  }
}
