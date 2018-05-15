# FindEAT-api

## Methods
#### search
Search restaurants from coordinate or location

Parameters:
- tipo  [required] - choice between 'gps' or 'luogo'
- lista [required] - place the position 'lat','lng' or name of city/town


Example Usage:
```
findeat-api.herokuapp.com/?tipo=gps&lista=43.7292325,12.6129107
```
Returns
The standard JSON array

- name
- adress
- location
- opening
- telephone number
- website
- rating
- times
