import numpy as np
import pandas as pd
import collections

condition='rtft' #random, tft, rtft
depth = 5

datafile = condition+'.txt'
outputfileA = 'mcc_probabilities_v3/' + condition + '_mcc_probabilities_moodA_depth_' + str(depth) + '.txt'
outputfileB = 'mcc_probabilities_v3/' + condition + '_mcc_probabilities_moodB_depth_' + str(depth) + '.txt'

num_rounds_per_subject = 60
skipcount = 10 #Skip these many initial rows for each subject

df = pd.read_csv(datafile, sep='\t')

history = collections.deque([])
subject_action = ''
count_a = 0
counts_a_after_a = np.zeros(depth+1)
counts_a_after_b = np.zeros(depth+1)
total_counts_after_a = np.zeros(depth+1, dtype=int)
total_counts_after_b = np.zeros(depth+1, dtype=int)

num_skipped = 0
for index, row in df.iterrows():
    if (index%num_rounds_per_subject==0):
        num_skipped = 0
        history = collections.deque([])
        count_a = 0
    
    if (num_skipped < skipcount):
        num_skipped += 1
        continue
    
    if (len(history)<depth):
        history.append(row['oppDecision'])
        subject_action = row['subjectDecision']
        if (row['oppDecision']=='A'):
            count_a += 1
        continue
    
    if (subject_action=='A'):
        if (row['subjectDecision']=='A'):
            counts_a_after_a[count_a] += 1
            total_counts_after_a[count_a] += 1
        else:
            total_counts_after_a[count_a] += 1
    else:
        if (row['subjectDecision']=='A'):
            counts_a_after_b[count_a] += 1
            total_counts_after_b[count_a] += 1
        else:
            total_counts_after_b[count_a] += 1
    
    if (history.popleft()=='A'):
        count_a -= 1
    
    if (row['oppDecision']=='A'):
        count_a += 1
        
    history.append(row['oppDecision'])
    subject_action = row['subjectDecision']
    
total_counts_after_a[total_counts_after_a == 0] = -1
total_counts_after_b[total_counts_after_b == 0] = -1

prob_a_after_a = counts_a_after_a/total_counts_after_a
prob_a_after_b = counts_a_after_b/total_counts_after_b

bin_std_after_a = np.sqrt(prob_a_after_a*(1-prob_a_after_a)/total_counts_after_a)
bin_std_after_b = np.sqrt(prob_a_after_b*(1-prob_a_after_b)/total_counts_after_b)
    
fw = open(outputfileA, "w")

fw.write("fraction_A,probA_afterA,bin_stdA_afterA\n")
for i in range(depth+1):
    fw.write(str(i/depth) + "," + str(prob_a_after_a[i]) + "," + str(bin_std_after_a[i]) + "\n")
    
fw.close()

fw = open(outputfileB, "w")
fw.write("fraction_A,probA_afterB,bin_stdA_after_B\n")
for i in range(depth+1):
    fw.write(str(i/depth) + "," + str(prob_a_after_b[i]) + "," + str(bin_std_after_b[i]) +"\n")
    
fw.close()


# fw.write("fraction_A,countA_afterA,total_afterA,probA_afterA,bin_stdA_after_A,countA_afterB,total_afterB,probA_afterB,bin_stdA_after_B\n")
# for i in range(depth+1):
#     fw.write(str(i/depth) + "," + str(counts_a_after_a[i]) + "," + str(total_counts_after_a[i]) + "," + str(prob_a_after_a[i]) + \
#              "," + str(bin_std_after_a[i]) + "," + str(counts_a_after_b[i]) + "," + str(total_counts_after_b[i]) + "," + \
#              str(prob_a_after_b[i]) + "," + str(bin_std_after_b[i]) +"\n")
    
# fw.close()
    