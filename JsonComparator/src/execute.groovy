 import Compare

 Compare.compare {

    result  '''  { "name":"John2 ", "age":31, "city":"New York" }'''
    expectedResult '''  { "name":"John", "age":31, "city":"New York" }'''
    iGNORING_ARRAY_ORDER false
    iGNORING_EXTRA_ARRAY_ITEMS false
    iGNORING_EXTRA_FIELDS false
    iGNORING_VALUES false
   // ignoringFields "name"
    comparePlease

}