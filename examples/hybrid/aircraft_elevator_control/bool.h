
#define set_bool(var, C) \
  if (C) var := 1 else var := 0 endif

#define bool(var) \
  var > 0