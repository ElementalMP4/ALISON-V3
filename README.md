# ALISON
Automatic Learning Intelligent Sentence Organising Network

ALISON is a data-centric sentence generator that uses Discord guild messages to continuously learn how to create imitation messages.

ALISON uses:
- Java because who doesn't love Java?
- PostrgeSQL as a database
- JDA to talk to Discord
- Spring because it's just so damn convenient
- AFINN-111 for sentiment analysis
- Magic

## What ALISON *isn't*

ALISON is not a chat bot. She will not have a conversation with you, and she never will. 

If you are looking for an awesome chat bot, please visit my good friend [Scot_Survivor](https://github.com/Scot-Survivor) over at the [Gavin Development](https://github.com/Gavin-Development) corner instead.

## What ALISON *is*

ALISON is a series of different NLP technologies crammed together into one bot. None of them are particularly complex, anyone could understand the theory in just a few moments. Having said that, here's the theory:

### A Markov Chain Sentence Generator

The pride and joy of ALISON's ensemble is her ability to learn from your Discord messages and speak like you. She does this by following a very simple algorithm.

Learning is easy:

1) Receive a text message
2) Split the message into tokens (individual words)
3) Add the tokens to a database

Generation is a bit more involved:

1) Pick a random start token
2) Using this token, get a list of *all* the tokens in the database for this user which start with this token
3) Create a list which contains all these tokens, where each token occurs `n` times, `n` being the number of times you have used this combination before.
4) Pick a random next token from this list
5) Repeat until we reach a stop word
6) Send the sentence back to the user

ALISON's markov chain implementation is by no means the first of its kind, however I refer to the algorithm as WRTT (**W**eighted **R**andom **T**ree **T**raversal)

### A Sentiment Analyser

ALISON's Sentiment Analyser is based around the AFINN-111 dataset. It contains ~3000 words with scores between -5 and 5. To score a piece of text, we can simply search the text for any occurrences of a known, scored word or phrase and do some mathematical goodness. **Because who doesn't love making up random equations they don't understand and calling it AI?**

As before, here is a breakdown of the sentiment algorithm:

1) Remove all non-alphabetic chars from the text
2) Iterate through the list of known words. Each time a word is found in the text, add this word to a list `n` times, where `n` is the number of occurrences of this word.
3) Classify the newly created list into two more lists, `positive` and `negative`

Once all this goodness is done, we calculate several scores. `Total`, `Average` and `Adjusted`.

`Total`: gives an overall score for a piece of text based on the positives and negatives

Total is easy to calculate, just add up all the positive and negative scores.


`Average`: calculates the average score for a piece of text

Average is also easy, just add them up as before and divide by the number of positives and negatives.

`Adjusted`: gives an overall score but will benefit or detriment a score based on the ratio of positives to negatives (used to make a sentiment decision)

Adjusted is a bit more involved...

If there are no positives or negatives, the score is 0.
If there are no positives but some negatives, add the number of negatives multiplied by -1 to the total score
If there are no negatives but some positives, add the number of positives multiplied by +1 to the total score
If there are more positives than negatives, calculate the number of positives minus the number of negatives, then multiply by +1 and add to the total score
If there are more negatives than positives, calculate the number of negatives minus the number of positives, then multiply by -1 and add to the total score

This algorithm is probably not quite as good as IBM Watson (I wonder why?) but it does the job in an entertaining way so I am not particularly bothered. I name this method AASC (**A**djusted **A**FINN **S**entiment **C**alculation)

### A Levenshtein Calculator

This piece of NLP goodness serves only one purpose. Help people if they misspell a command. It works a bit like this:

1) A user enters a command which has been spelt wrong
2) Alison compares this command name to every known command name
3) She finds the levenshtein distance between this command and the known commands
4) The first one which has a levenshtein distance <= 2 will be chosen as a suggestion

### Conclusion

Thus concludes this epic tale, A Research Paper Called ALISON. I am not a data scientist, nor am I in any way an AI/ML or NLP expert. I am fully aware that ALISON is not very smart, but I personally think this makes her more entertaining. ALISON is not meant to be accurate, she's meant to be entertaining.

If you want to see her working, you will need to run your own instance as mine is not publicly available for privacy reasons.

## Running ALISON

Running ALISON is not a difficult task, however you will need some prerequisites. I recommend:

- Eclipse IDE (to compile ALISON)
- Docker (to run PostgreSQL)
- Java SE 8

### Building

1) Pull or clone this repository
2) Import the pom.xml into Eclipse
3) Create a build run config with the goals `clean package --debug` for ALISON
4) Build ALISON using this run config

### Running

Before you can run ALISON, whether from a .jar or from Eclipse, you will need a database and a config file. Database migrations are handled for you by Hibernate.

I **strongly** recommend using Docker for development/testing. It's a lot easier than running a PostgreSQL server. Follow [this guide](https://dev.to/shree_j/how-to-install-and-run-psql-using-docker-41j2)

When you have a database server running, use PGAdmin to create a database called `Alison`.

Your config file needs to be called `AlisonConfig.properties` and it needs to be placed at either the root of the project, or directly next to a compiled jar. It should contain these fields:

```
defaultPrefix=[a prefix]
token=[Discord bot token]
hibernate.User=[Postgres username]
hibernate.Password=[Postgres password]
master=[Your Discord ID]
```

With this file created and in the right place, Alison should now come crashing into existence for your enjoyment.

Alison does not come with any tech support whatsoever. Good luck!