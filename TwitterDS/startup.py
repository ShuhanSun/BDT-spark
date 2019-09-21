from twython import Twython
# from LSTM import predictLSTM
import json
from kafka import KafkaProducer
import time

def tweetLearning(keyword,count,result_type):
    #json_ = request.json
    count = 10

    credentials = {}
    credentials['CONSUMER_KEY'] = "pyKBZeHhRb9w006Cv4Ituqi7E"
    credentials['CONSUMER_SECRET'] = "DwUyJXTzPF4FexqztrXKu6ij7tf9GHnH9EiGGh0FGtdgAejjux"
    credentials['ACCESS_TOKEN'] = "53697152-Nvdb7vmwGzPUI65duv6fkkA5ejvC8JSt0FtZncz3J"
    #credentials['ACCESS_SECRET'] = "AlQ0boi3ePIY3muIH6DHylCidVyMkEtI56hI9cin2C7wx"
    credentials['ACCESS_SECRET'] = "EEQDKBqGcuuUjQRs6sc4Fa6WScV67kmWblcMd2IGCEnOP"
    python_tweets = Twython(credentials['CONSUMER_KEY'], credentials['CONSUMER_SECRET'])

    # Create our query
    query = {'q': keyword,
            'result_type': result_type,  #'popular' recent mixed
            'count': count,
            'lang': 'en',
            }

    # Search tweets
    #dict_ = {'user': [], 'date': [], 'text': [], 'favorite_count': []}
    
    #d['hashtags'] = [hashtag['text'] for hashtag in tweet['entities']['hashtags']]
    #d.get('favorite_count',list).append(tweet['favorite_count'])
    #producer = KafkaProducer(bootstrap_servers='localhost:9092',)
    producer = KafkaProducer(bootstrap_servers='localhost:9092', request_timeout_ms=1000000, api_version_auto_timeout_ms=1000000)
    #producer.send('TutorialTopic', b'Hello, World!')

    while True:

    	for tweet in python_tweets.search(**query)['statuses']:
		# print("1----------------------------------------")
		# print(tweet['text'], "\r\n")
		# print("2----------------------------------------")
		# print(tweet['user']['screen_name'], "\r\n")
		# print("3----------------------------------------")
		# print(tweet['user']['location'], "\r\n")
		y = json.dumps(tweet)
		#producer.send('TutorialTopic', key=b'message-two', value=b'This is Kafka-Python')
		producer.send('TutorialTopic', y)
		producer.flush()
		print(y)
	time.sleep(2)

if __name__ == '__main__':
    tweetLearning('Trump',10,'mixed')
