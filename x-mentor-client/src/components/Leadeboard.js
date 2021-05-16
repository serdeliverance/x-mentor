import React from 'react'
import { Typography ,Divider, Grid, makeStyles } from '@material-ui/core'
import { Bar } from 'react-chartjs-2';

const useStyles = makeStyles(() => ({
  leaderboard: {
    padding: "2rem 4rem",
    width: "100%"
  }
}));

export default function Leaderboard({scores}) {
  const classes = useStyles()

  return (
    <Grid container item xs={6}>
      <Grid item className={classes.leaderboard}>
          <Typography variant="h6">Leaderboard</Typography>
          <Divider />
          <Bar
            width={20}
              height={5}
              data={{
                labels: scores[1],
                datasets: [{ 
                  label: 'Seconds',
                  data: scores[0],
                  backgroundColor: [
                    'rgba(255, 99, 132, 0.2)',
                    'rgba(54, 162, 235, 0.2)',
                    'rgba(255, 206, 86, 0.2)',
                    'rgba(75, 192, 192, 0.2)',
                    'rgba(153, 102, 255, 0.2)',
                    'rgba(255, 159, 64, 0.2)',
                  ],
                  borderColor: [
                    'rgba(255, 99, 132, 1)',
                    'rgba(54, 162, 235, 1)',
                    'rgba(255, 206, 86, 1)',
                    'rgba(75, 192, 192, 1)',
                    'rgba(153, 102, 255, 1)',
                    'rgba(255, 159, 64, 1)',
                  ],
                  borderWidth: 1,
                }]
              }}
              options={{
                indexAxis: 'y',
                elements: {
                  bar: { borderWidth: 2 },
                },
                plugins: {
                  legend: { display: false, },
                  title: {
                    display: true,
                    text: 'Watched seconds per student',
                  },
                },
                scales: {
                  y: { max: 4 }
                }
              }}
          />
      </Grid>
  </Grid>
  )
}