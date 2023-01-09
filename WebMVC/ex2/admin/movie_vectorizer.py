import pandas as pd
import numpy as np
from sentence_transformers import SentenceTransformer

np.set_printoptions(suppress=True)

df = pd.read_csv("movie_dataset.csv")

features = ['keywords', 'cast', 'genres', 'director']
for feature in features:
    df[feature] = df[feature].fillna('')
	
def combined_features(row):
    return row['keywords']+" "+row['cast']+" "+row['genres']+" "+row['director']
df["combined_features"] = df.apply(combined_features, axis =1)

model = SentenceTransformer('msmarco-MiniLM-L-6-v3')
df["vector_descriptions"] = np.ndarray((df.shape[0]), dtype=list).fill([])

for i in range(df.shape[0]):
	df.iat[i, len(df.columns)-1] = model.encode(df["combined_features"][i])

out_df = pd.DataFrame()
out_df["original_title"] = df["original_title"]
out_df["vector_descriptions"] = df["vector_descriptions"]

out_df.to_csv('vectorized_dataset.csv', sep=';',index=False)