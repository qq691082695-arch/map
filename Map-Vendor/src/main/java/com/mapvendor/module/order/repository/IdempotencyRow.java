package com.mapvendor.module.order.repository;
public class IdempotencyRow {
 private Long id; private String requestHash; private String responseRef; private String status;
 public Long getId(){return id;} public void setId(Long v){id=v;} public String getRequestHash(){return requestHash;} public void setRequestHash(String v){requestHash=v;}
 public String getResponseRef(){return responseRef;} public void setResponseRef(String v){responseRef=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;}
}
