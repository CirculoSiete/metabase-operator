package com.circulosiete.k8s.operator.metabase;

import io.micronaut.runtime.Micronaut;
import lombok.extern.slf4j.Slf4j;

public class MetabaseOperatorApplication {

  public static void main(String[] args) throws InterruptedException {
    Micronaut.run(MetabaseOperatorApplication.class, args);
  }
}
