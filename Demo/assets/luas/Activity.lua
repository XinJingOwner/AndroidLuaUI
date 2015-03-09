-----------------------------------------------------------------------------
-- Activity class.
-- Author: lizhennian
-- Version: 0.0.1
-- USAGE:
-- CHANGELOG:
--     0.0.1 2015-03-02 Create Activity class.
-----------------------------------------------------------------------------

import "android.util.*"
import "android.content.*"

Activity = {
  tag = "lua_activity",
  context = nil
}

function Activity:attachContext(ctx)
  Log:i(self.tag, "attachContext")
  self.context = ctx
end

function Activity:onPause(t)
  Log:i(self.tag, "onPause")
end

function Activity:onCreate(savedInstanceState)
  Log:i(self.tag, "onCreate")
end

function Activity:onRestart()
  Log:i(self.tag, "onRestart")
end

function Activity:onStart()
  Log:i(self.tag, "onStart")
end

function Activity:onResume()
  Log:i(self.tag, "onResume")
end

function Activity:onPause()
  Log:i(self.tag, "onPause")
end

function Activity:onStop()
  Log:i(self.tag, "onStop")
end

function Activity:onDestroy()
  Log:i(self.tag, "onDestroy")
end

function Activity:onBackPressed()
  Log:i(self.tag, "onBackPressed")
end

function Activity:setContentView(name)
  local resId = self:getResourceId(name, "layout")
  self.context:setContentView(resId)
end

function Activity:getView(name)
  local resId = self:getResourceId(name, "id")
  return self.context:findViewById(resId)
end

function Activity:getResourceId(name, resourceType)
  return self.context:getResources():getIdentifier(name, resourceType, self.context:getPackageName())
end

function Activity:startActivity(className)
  local intent = Intent();
  intent:setClassName(this.getPackageName(), className)
  self.context:startActivity(intent);
end

function Activity:new(t)
  t = t or {}
  setmetatable(t, self)
  self.__index = self

  return t
end

return Activity
