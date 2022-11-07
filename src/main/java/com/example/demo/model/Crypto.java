package com.example.demo.model;

import com.opencsv.bean.CsvBindByName;

import lombok.Data;

@Data
public class Crypto {

    @CsvBindByName(column = "timestamp")
    private Long timeStamp;
    @CsvBindByName(column = "symbol")
    private String symbol;
    @CsvBindByName(column = "price")
    private Double price;
  
}
