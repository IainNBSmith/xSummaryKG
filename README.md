# xSummaryKG

Compare information stored in summary and original articles to evaluate summarization quality. 


Requirements:


[XTE](https://github.com/ssvivian/XTE)                 - (Entailment)

[StanfordNLP OpenIE](https://nlp.stanford.edu/software/openie.html)  - (Open Information Extraction)

[WS4J](https://github.com/dmeoli/WS4J)                - (Semantic Similarity)


Usage:

To use load all required libraries in preferred IDE (IntellijIdea recommended). Run the files summary/src/Main.java

Replace the named files in main function to change the compared files. Outputs all the relations from both sources, missing 
relationships, precision, recall, and relation ratio.




Articles Used:


[dailyshow](https://www.washingtonpost.com/entertainment/tv/2024/01/24/jon-stewart-return-daily-show/) - Washington Post

[hockey](https://www.stonyplainreporter.com/news/local-news/kids-helping-kids-worlds-longest-hockey-game-juniors-raises-400k-for-paediatric-cancer-research)    - Stony Plain Reporter

[gunn](https://www.stonyplainreporter.com/news/local-news/gunn-man-woman-charged)      - Stony Plain Reporter

[trump](https://www.stonyplainreporter.com/entertainment/celebrity/trump-allies-vow-holy-war-against-taylor-swift-over-biden-endorsement)     - Stony Plain Reporter