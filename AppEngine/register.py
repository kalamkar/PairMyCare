#!/usr/bin/python2.7
#-------------------------------------------------------------------------------
# Copyright 2013 Abhijit Kalamkar. All rights reserved.
#-------------------------------------------------------------------------------

""" Register module
"""

__author__ = 'abhi@teddytab.com (Abhijit Kalamkar)'

import webapp2
from google.appengine.ext.db import GqlQuery
from google.appengine.ext.db import Key

from datastore import Device

class Register(webapp2.RequestHandler):

    def get(self):
        str_id = self.request.get('id')
        token = self.request.get('token')
        
        if not str_id or not token:
            self.response.status_int = 400
            self.response.body = "Invalid or missing id and or token."
            return
            
        objects = GqlQuery('SELECT * FROM Device WHERE __key__ = :1',
                           Key.from_path('Device', str_id))
        
        if objects.count() < 1:
            obj = Device(key=Key.from_path('Device', str_id), token=token)
        else:
            obj = objects.get()
        
        obj.put()
        
        self.response.status_int = 200
        self.response.body = "OK"
        
    def post(self):
        self.get();

