#!/usr/bin/python2.7
#-------------------------------------------------------------------------------
# Copyright 2013 Abhijit Kalamkar. All rights reserved.
#-------------------------------------------------------------------------------


""" Datastore classes
"""

__author__ = 'abhi@teddytab.com (Abhijit Kalamkar)'


from google.appengine.ext import db

class Device(db.Expando):
    token = db.StringProperty(required=True)
    update_time = db.DateTimeProperty(auto_now=True)
    create_time = db.DateTimeProperty(auto_now_add=True)
