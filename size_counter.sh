#!/bin/bash
URL="https://upload.wikimedia.org/wikipedia/commons/f/fa/%22_12_-_ITALY_-_artichoke_in_bloom_-_BIO_high_quality_food_%28organic_farming%29.JPG"
curl -sI $URL | grep Content-Length | awk '1. {print $2}'

URL="http://publicdomainarchive.com/?ddownload=47405"
curl -sI $URL | grep Content-Length | awk '2. {print $2}'

URL="https://www.dropbox.com/s/r5l8t0nfjb3rp3b/135H.jpg?dl=1"
curl -sI $URL | grep Content-Length | awk '3. {print $2}'

URL="http://download.thinkbroadband.com/20MB.zip"
curl -sI $URL | grep Content-Length | awk '4. {print $2}'

URL="http://www.rngresearch.com/download/block0.rng"
curl -sI $URL | grep Content-Length | awk '5. {print $2}'

URL="http://iliketowastemytime.com/system/files/snub-nosed-monkey-hd-wallpaper.jpg?download=1"
curl -sI $URL | grep Content-Length | awk '6. {print $2}'

URL="http://www.mellowholidays.com/Views/Uploads/FrontImage/893512536-134037High_Resolution_Sunset_by_thereal7.jpg"
curl -sI $URL | grep Content-Length | awk '7. {print $2}'

URL="http://mirror.internode.on.net/pub/test/10meg.test"
curl -sI $URL | grep Content-Length | awk '8. {print $2}'

URL="http://qbrushes.net/wp-content/plugins/download-monitor/download.php?id=13+Cloud+Brushes"
curl -sI $URL | grep Content-Length | awk '9. {print $2}'

URL="http://www.noao.edu/image_gallery/images/d5/suns.tiff"
curl -sI $URL | grep Content-Length | awk '10. {print $2}'

URL="http://upload.wikimedia.org/wikipedia/commons/6/6c/High-quality_C._pepo_fruit_specimen.JPG"
curl -sI $URL | grep Content-Length | awk '11. {print $2}'

URL="http://cdn4.tricksmachine.com/wp-content/uploads/2011/06/two_worlds.jpg"
curl -sI $URL | grep Content-Length | awk '12. {print $2}'
