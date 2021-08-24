data = [50,50,47,97,49,3,53,42,26,74,82,62,37,15,70,27,36,35,48,52,63,64]
print(data)

import numpy as np

grades = np.array(data)
print(grades)

print (type(data),'x 2:', data * 2)
print('---')
print (type(grades),'x 2:', grades * 2)
