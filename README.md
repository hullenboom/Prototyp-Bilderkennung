# Prototyp Bilderkennung

Dieser Prototyp in Form einer Android Applikation ist im Rahmen einer Masterarbeit mit dem Titel "Einsatzpotenziale von Machine Learning zur Bilderkennung im Einzelhandel – Prototypische Entwicklung einer mobilen Applikation" an der Universität Duisburg-Essen im Studiengang Wirtschaftsinformatik entstanden.

Die Applikation ermöglicht die Erkennung von den drei Einzelhandelsprodukten Chipsfrisch, Lätta und Nutella.
Die Bilderkennung wurde mit TensorFlow Lite umgesetzt. Dafür wurde ein Image Classification und ein Object Detection Modell trainiert.

## Trainieren der verwendeten Modelle

Für das Trainieren der Modelle wurden folgende Google Colab Notebooks verwendet:

Image Classification ohne Augmentationen: https://colab.research.google.com/drive/1ZRZ87s2DDaclUcjmq4X6zAMv1VfzKal-
Image Classification mit Augmentationen: https://colab.research.google.com/drive/1epQCqC2NwXhyU7gkIJIM8F6_-1w7sCD3
Object Detection: https://colab.research.google.com/drive/1ADGhIlj23ioQeOYKQcNMdJDAd4YS_bVi
