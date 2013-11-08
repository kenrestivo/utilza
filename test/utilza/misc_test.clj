(ns utilza.misc-test
  (:use clojure.test
        utilza.misc))


(deftest assure-histogram
  (let [test-data '([1 9463] [2 8819] [3 8372] [4 8025] [5 7715] [6 7448] [7 7186] [8 6952] [9 6736] [10 6543] [11 6376] [12 6199] [13 6034] [14 5886] [15 5752] [16 5632] [17 5523] [18 5414] [19 5303] [20 5183] [21 5078] [22 4990] [23 4902] [24 4827] [25 4737] [26 4674] [27 4592] [28 4525] [29 4463] [30 4398] [31 4318] [32 4259] [33 4204] [34 4172] [35 4119] [36 4075] [37 4023] [38 3964] [39 3921] [40 3871] [41 3825] [42 3779] [43 3737] [44 3683] [45 3651] [46 3609] [47 3566] [48 3525] [49 3495] [50 3459] [51 3417] [52 3385] [53 3345] [54 3319] [55 3290] [56 3259] [57 3219] [58 3178] [59 3144] [60 3114] [61 3079] [62 3046] [63 3022] [64 3005] [65 2978] [66 2941] [67 2915] [68 2890] [69 2862] [70 2836] [71 2811] [72 2788] [73 2766] [74 2739] [75 2711] [76 2691] [77 2663] [78 2638] [79 2617] [80 2606] [81 2584] [82 2561] [83 2542] [84 2521] [85 2505] [86 2486] [87 2470] [88 2453] [89 2431] [90 2409] [91 2399] [92 2384] [93 2361] [94 2344] [95 2330] [96 2312] [97 2292] [98 2268] [99 2252] [100 2233])
        sample-result '([100 2233] [99 4485] [98 6753] [97 9045] [96 11357] [95 13687] [94 16031] [93 18392] [92 20776] [91 23175] [90 25584] [89 28015] [88 30468] [87 32938] [86 35424] [85 37929] [84 40450] [83 42992] [82 45553] [81 48137] [80 50743] [79 53360] [78 55998] [77 58661] [76 61352] [75 64063] [74 66802] [73 69568] [72 72356] [71 75167] [70 78003] [69 80865] [68 83755] [67 86670] [66 89611] [65 92589] [64 95594] [63 98616] [62 101662] [61 104741] [60 107855] [59 110999] [58 114177] [57 117396] [56 120655] [55 123945] [54 127264] [53 130609] [52 133994] [51 137411] [50 140870] [49 144365] [48 147890] [47 151456] [46 155065] [45 158716] [44 162399] [43 166136] [42 169915] [41 173740] [40 177611] [39 181532] [38 185496] [37 189519] [36 193594] [35 197713] [34 201885] [33 206089] [32 210348] [31 214666] [30 219064] [29 223527] [28 228052] [27 232644] [26 237318] [25 242055] [24 246882] [23 251784] [22 256774] [21 261852] [20 267035] [19 272338] [18 277752] [17 283275] [16 288907] [15 294659] [14 300545] [13 306579] [12 312778] [11 319154] [10 325697] [9 332433] [8 339385] [7 346571] [6 354019] [5 361734] [4 369759] [3 378131] [2 386950] [1 396413])]
    (is (= (make-histogram-cumulative test-data) sample-result))))


  (comment
    (run-tests)
    

    )



