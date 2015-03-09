-----------------------------------------------------------------------------
-- splash_activity.lua
-- Create by lizhennian on 2015-02-03
-----------------------------------------------------------------------------

import "android.animation.*"
import "android.view.animation.*"
import "android.os.*"
import "android.view.*"
import "android.util.*"
import "android.widget.*"
import "java.lang.*"

require("Activity")

TestActivity = Activity:new({
  tag = "lua_test",
})

function TestActivity:onCreate(savedInstanceState)
  Log:i(self.tag, "onCreate")
end

function TestActivity:onStart()
  Log:i(self.tag, "onStart")
end

return TestActivity
