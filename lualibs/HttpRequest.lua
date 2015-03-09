-----------------------------------------------------------------------------
-- HTTP Module.
-- Author: lizhennian
-- Version: 0.0.1
-- USAGE:
-- CHANGELOG:
--     0.0.1 2015-02-28 Create HTTP module.
-----------------------------------------------------------------------------

local tag = "lua.HttpRequest"

local function body(http, charset)
  local httpRequest = nil

  local status, err = pcall(function()
    local htttpInstance = http['httpInstance']
    httpRequest = htttpInstance:post(url)
  end)

  if not status then
  --TODO
  end

  return httpRequest
end

local function followRedirects(http, isfollow)
  local httpRequest = nil

  local status, err = pcall(function()
    local htttpInstance = http['httpInstance']
    httpRequest = htttpInstance:followRedirects(isfollow)
  end)

  if not status then
  --TODO
  end

  return httpRequest
end

local function acceptCharset(http, charset)
  local httpRequest = nil

  local status, err = pcall(function()
    local htttpInstance = http['httpInstance']
    httpRequest = htttpInstance:acceptCharset(charset)
  end)

  if not status then
  --TODO
  end

  return httpRequest
end

local function newInstance(url, method)
  local javaHttpRequest = nil
  local javaHttpInstance = nil
  local http = {}

  local status, err = pcall(function()

      javaHttpRequest = luajava.bindClass("com.android.luajava.htpp.HttpRequest")
      javaHttpInstance = javaHttpRequest:newInstance(url, method)
      http['httpInstance'] = javaHttpInstance
      http['followRedirects'] = followRedirects
      http['acceptCharset'] = acceptCharset
      http['body'] = body

  end)

  if not status then
  --TODO
  end

  return http
end


do
  local modname = 'HttpRequest'
  local M = {
    ['newInstance'] = newInstance
  }
  _G[modname] = M
  package.loaded[modname] = M
  setfenv(1, M)

  return M
end

