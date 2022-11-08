package com.example.demo.model;

import com.opencsv.bean.CsvBindByName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Crypto {

    @CsvBindByName(column = "timestamp")
    private Long timeStamp;
    @CsvBindByName(column = "symbol")
    private String symbol;
    @CsvBindByName(column = "price")
    private Double price;
  
}
