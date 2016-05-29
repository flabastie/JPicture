# JPicture

Usage IndexDatabase.java
========================


Lancement indexation
====================

- Dans fonction main de JPictureNFE205 :

- Décommenter la ligne correspondante à l'indexation à lancer :
 (ex : // new IndexDatabase(1, 16, "Base10000_files.txt", "HistGREY_16.json"); )
 Paramètre fichier pris en entrée : Base10000_files.txt
 Paramètre fichier en sortie : HistGREY_16.json


Format utilisé pour stockage des signatures : json
==================================================

Exemple de formatage pour HistGREY_16.json :
La clé représente le numéro d'image.
Les pourcentages sont placés dans un tableau.

{"187088":
	[
		13.312785,
		11.323039,
		10.497029,
		10.006714,
		9.376018,
		12.309773,
		13.667806,
		11.038208,
		3.8899739,
		1.3407389,
		0.76904297,
		0.4079183,
		0.41503906,
		1.3865153,
		0.25634766,
		0.0030517578
	], .......
	.......
}

