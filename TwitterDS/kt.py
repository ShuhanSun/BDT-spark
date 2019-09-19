from kafka import KafkaProducer
#producer = KafkaProducer(bootstrap_servers='localhost:9092',)
producer = KafkaProducer(bootstrap_servers='localhost:9092', request_timeout_ms=1000000, api_version_auto_timeout_ms=1000000)
#producer.send('TutorialTopic', b'Hello, World!')
producer.send('TutorialTopic', key=b'message-two', value=b'This is Kafka-Python')
producer.flush()
