# Linfinitype Speed and Accuracy test data analytics
# Copyright (C) 2022 Kovacs Lorand, Linfinity Technologies
# 
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU Lesser General Public License as published by the Free
# Software Foundation, either version 3 of the License, or (at your option) any
# later version.
# 
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU General Lesser Public License for more
# details.
# 
# You should have received a copy of the GNU Lesser General Public License along
# with this program.  If not, see <http://www.gnu.org/licenses/>.

import os
import sys
import datetime
import time
import glob

def get_timestamp(ln):
    return datetime.datetime.strptime(ln[1:24], '%Y-%m-%d %H:%M:%S.%f')

doublecheck = ''
previous = ''

wordcount = 0
currentword = 0
mistakes = []
wordtimes = []
wordlengths = []
firstchar = False

def sim_input(gesture, now):
    global doublecheck
    global previous
    global currentword
    global mistakes
    global wordtimes
    global wordlimit
    global firstchar
    global wordcount
    global wordlengths
    if gesture != doublecheck:
        previous = doublecheck
        doublecheck = gesture
    elif previous != doublecheck:
        previous = gesture
        if gesture == '$' or gesture == ' ':
            if len(wordtimes) > currentword:
                sys.stdout.write('\n')
                sys.stdout.flush()
                wordtimes[currentword] = now - wordtimes[currentword]
                currentword += 1
                wordcount += 1
                firstchar = False
        elif gesture == '-':
            if len(mistakes) > currentword:
                sys.stdout.write('\b \b')
                sys.stdout.flush()
                mistakes[currentword] += 1
                wordlengths[currentword] -= 1
        else:
            if not firstchar:
                wordtimes.append(now)
                mistakes.append(0)
                wordlengths.append(0)
                firstchar = True
            wordlengths[currentword] += 1
            sys.stdout.write(gesture)
            sys.stdout.flush()
        
csv = ["Test,Words,Avg length,Total time,Avg time/word,WPM,Avg CPM,Avg mistakes\n"]

def analyze_data(filename):
    global doublecheck
    global previous
    global currentword
    global mistakes
    global wordtimes
    global wordlimit
    global firstchar
    global wordcount
    global wordlengths
    global csv

    doublecheck = ''
    previous = ''
    wordcount = 0
    currentword = 0
    mistakes = []
    wordtimes = []
    wordlengths = []
    firstchar = False

    fcntl = []
    
    with open(filename, "r") as f:
        fcntl = f.readlines()

    for i in range(1, len(fcntl)):
        sim_input(fcntl[i][26], get_timestamp(fcntl[i]))

    avg_mistakes = 0
    avg_time = 0
    avg_cpm = 0
    avg_wordlen = 0
    wpm = wordcount
    total_time = 0

    for i in range(0, wordcount):
        avg_mistakes += mistakes[i]
        avg_time += wordtimes[i].total_seconds()
        avg_cpm += wordlengths[i]

    avg_wordlen = avg_cpm
    total_time = avg_time
    avg_wordlen /= wordcount
    avg_cpm /= (total_time / 60)
    wpm /= (total_time / 60)
    avg_time /= wordcount
    avg_mistakes /= wordcount

    print("== DATA ANALYTICS ==")
    print("Words: " + str(wordcount))
    print("Average word length: " + str(avg_wordlen) + " characters")
    print("Total time: " + str(total_time) + " sec")
    print("Average time per word: " + str(avg_time) + " sec")
    print("Words per minute: " + str(wpm))
    print("Average characters per minute: " + str(avg_cpm))
    print("Average count of mistakes per word: " + str(avg_mistakes))
    csv.append(filename + "," + str(wordcount) + "," + str(avg_wordlen) + "," + str(total_time) + "," + str(avg_time) + "," + str(wpm) + "," + str(avg_cpm) + "," + str(avg_mistakes) + "\n")

print("== Linfinitype Test Data Analytics Tool ==")

for file in glob.glob("*.log"):
    analyze_data(file)

with open("analytics.csv", "w") as f:
    f.writelines(csv)

print("Analytics saved as analytics.csv")