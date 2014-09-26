#!/usr/bin/python2.7
#-------------------------------------------------------------------------------
# Copyright 2013 Abhijit Kalamkar. All rights reserved.
#-------------------------------------------------------------------------------

""" Message module
"""

__author__ = 'abhi@teddytab.com (Abhijit Kalamkar)'

import json
import urllib
import webapp2
from google.appengine.api import urlfetch
from google.appengine.ext.db import GqlQuery
from google.appengine.ext.db import Key

from datastore import Device

ANDROID_GCM_URL = 'https://android.googleapis.com/gcm/send'
API_KEY = 'AIzaSyCks1Y1Z6Zr4nFcm-2m-2ecrDBmk7it7sc'

class Message(webapp2.RequestHandler):

    def get(self):
        str_id = self.request.get('id')
        message = self.request.get('message')
        
        if not str_id or not message:
            self.response.status_int = 400
            self.response.body = "Invalid or missing id and or message."
            return
            
        devices = GqlQuery('SELECT * FROM Device WHERE __key__ = :1',
                           Key.from_path('Device', str_id))
        
        if devices.count() < 1:
            self.response.status_int = 404
            self.response.body = "Target device not registered."
            return
        
        payload = {'registration_ids': [devices.get().token], 'data': {'message': message}}
        headers = {'Content-Type': 'application/json',
                   'Authorization': 'key=' + API_KEY}
        result = urlfetch.fetch(url = ANDROID_GCM_URL,
                                payload = json.dumps(payload),
                                method = urlfetch.POST,
                                headers = headers)
        
        self.response.status_int = result.status_code
        self.response.body = result.content
        
    def post(self):
        self.get();

