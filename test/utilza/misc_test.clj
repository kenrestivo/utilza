(ns utilza.misc-test
  (:use clojure.test
        utilza.misc))



  (def test-data '([1 9463] [2 8819] [3 8372] [4 8025] [5 7715] [6 7448] [7 7186] [8 6952] [9 6736] [10 6543] [11 6376] [12 6199] [13 6034] [14 5886] [15 5752] [16 5632] [17 5523] [18 5414] [19 5303] [20 5183] [21 5078] [22 4990] [23 4902] [24 4827] [25 4737] [26 4674] [27 4592] [28 4525] [29 4463] [30 4398] [31 4318] [32 4259] [33 4204] [34 4172] [35 4119] [36 4075] [37 4023] [38 3964] [39 3921] [40 3871] [41 3825] [42 3779] [43 3737] [44 3683] [45 3651] [46 3609] [47 3566] [48 3525] [49 3495] [50 3459] [51 3417] [52 3385] [53 3345] [54 3319] [55 3290] [56 3259] [57 3219] [58 3178] [59 3144] [60 3114] [61 3079] [62 3046] [63 3022] [64 3005] [65 2978] [66 2941] [67 2915] [68 2890] [69 2862] [70 2836] [71 2811] [72 2788] [73 2766] [74 2739] [75 2711] [76 2691] [77 2663] [78 2638] [79 2617] [80 2606] [81 2584] [82 2561] [83 2542] [84 2521] [85 2505] [86 2486] [87 2470] [88 2453] [89 2431] [90 2409] [91 2399] [92 2384] [93 2361] [94 2344] [95 2330] [96 2312] [97 2292] [98 2268] [99 2252] [100 2233]))

  (def sample-result '([1 396413] [2 386950] [3 378131] [4 369759] [5 361734] [6 354019] [7 346571] [8 339385] [9 332433] [10 325697] [11 319154] [12 312778] [13 306579] [14 300545] [15 294659] [16 288907] [17 283275] [18 277752] [19 272338] [20 267035] [21 261852] [22 256774] [23 251784] [24 246882] [25 242055] [26 237318] [27 232644] [28 228052] [29 223527] [30 219064] [31 214666] [32 210348] [33 206089] [34 201885] [35 197713] [36 193594] [37 189519] [38 185496] [39 181532] [40 177611] [41 173740] [42 169915] [43 166136] [44 162399] [45 158716] [46 155065] [47 151456] [48 147890] [49 144365] [50 140870] [51 137411] [52 133994] [53 130609] [54 127264] [55 123945] [56 120655] [57 117396] [58 114177] [59 110999] [60 107855] [61 104741] [62 101662] [63 98616] [64 95594] [65 92589] [66 89611] [67 86670] [68 83755] [69 80865] [70 78003] [71 75167] [72 72356] [73 69568] [74 66802] [75 64063] [76 61352] [77 58661] [78 55998] [79 53360] [80 50743] [81 48137] [82 45553] [83 42992] [84 40450] [85 37929] [86 35424] [87 32938] [88 30468] [89 28015] [90 25584] [91 23175] [92 20776] [93 18392] [94 16031] [95 13687] [96 11357] [97 9045] [98 6753] [99 4485] [100 2233]))


(deftest assure-histogram
  (is (= (make-histogram-cumulative test-data) sample-result)))


(comment
  (run-tests)
  

  )



