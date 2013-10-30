import android,tweepy,time

consumer_key = '0QMpjJqKINqJ0n8s9Q2Aw'
consumer_secret = 'WMUDV079mWQZ7HsQXyfexRl4OLBx22KCfffmoNhk'
access_key = '2165047417-HwaxNc10okIhmR32EHN8kI8NsPdWf4JZTb1Hz8W'
access_secret = 'tPHDQKT7GQ2Erm2MGpnvxqsIfbrcS5tlNpSM2LhNQ2xJM'


auth = tweepy.OAuthHandler(consumer_key, consumer_secret)
auth.set_access_token(access_key, access_secret)

api = tweepy.API(auth_handler=auth)

api.update_status('test')
