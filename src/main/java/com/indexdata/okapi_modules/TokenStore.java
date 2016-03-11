package com.indexdata.okapi_modules;

import java.util.List;

public interface TokenStore {
  public List<String> getTokenList(String store);
  public boolean hasStore(String store);
  public boolean hasToken(String token, String store);
  public void addStore(String store);
  public void addToken(String token, String store);
  public void deleteToken(String token, String store);
}
