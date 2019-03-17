# K-Nearest Neighbors (K-NN)

# Importing the libraries
import numpy as np
import pandas as pd

# Importing the dataset
dataset = pd.read_csv('Battery charge level - all.csv')
X = dataset.iloc[:, :-2].values
y = dataset.iloc[:, 3].values

from sklearn.model_selection import train_test_split
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size = 0.25, random_state = 0)

from sklearn.neighbors import KNeighborsClassifier
classifier = KNeighborsClassifier(n_neighbors = 5, metric = 'minkowski', p = 2)
classifier.fit(X_train, y_train)
score = classifier.score(X_test, y_test)

y_pred = classifier.predict(X_test)

# Making the Confusion Matrix
from sklearn.metrics import confusion_matrix
cm = confusion_matrix(y_test, y_pred)

# =============================================================================
# from sklearn_porter import Porter
# 
# porter = Porter(classifier, language='java')
# output = porter.export(embed_data=True)
# print(output)
# 
# Y_java = porter.predict(X_test)
# =============================================================================
