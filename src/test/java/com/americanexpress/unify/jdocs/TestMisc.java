package com.americanexpress.unify.jdocs;

import java.util.ArrayList;
import java.util.List;

public class TestMisc {

  public static void main(String[] args) {
    List<String> paths = new ArrayList<>();
    paths.add("$.individuals[00030].addresses[].cars[0098].model");
    paths = JsonPathUtils.getNoPaddedIndexes(paths);
    paths.stream().forEach(s -> System.out.println(s));
  }

}
