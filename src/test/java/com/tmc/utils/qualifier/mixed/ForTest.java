package com.tmc.utils.qualifier.mixed;

import com.tmc.connection.annotation.DatabaseProperty;

@DatabaseProperty(path = "o.properites", qualifiers = {"one", "two"})
public class ForTest {
}

@DatabaseProperty(path = "t.properites", qualifiers = {"three", "four"})
class ForTest2{

}
