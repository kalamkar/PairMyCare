#!/usr/bin/python2.7
#-------------------------------------------------------------------------------
# Copyright 2013 Abhijit Kalamkar. All rights reserved.
#-------------------------------------------------------------------------------

""" Main module
"""

__author__ = 'abhi@teddytab.com (Abhijit Kalamkar)'

import webapp2

from message import Message
from register import Register


app = webapp2.WSGIApplication([
                               ('/register', Register),
                               ('/message', Message)
                               ], debug=True)

