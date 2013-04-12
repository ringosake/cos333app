from flask import Flask
from flask.ext.sqlalchemy import SQLAlchemy
import os

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = "ec2-54-243-125-2.compute-1.amazonaws.com"
db = SQLAlchemy(app)